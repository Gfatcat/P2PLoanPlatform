package com.scut.p2ploanplatform.service.impl;

import com.scut.p2ploanplatform.dao.RepayPlanDao;
import com.scut.p2ploanplatform.entity.Guarantor;
import com.scut.p2ploanplatform.entity.Purchase;
import com.scut.p2ploanplatform.entity.RepayPlan;
import com.scut.p2ploanplatform.entity.User;
import com.scut.p2ploanplatform.enums.RepayPlanStatus;
import com.scut.p2ploanplatform.service.*;
import com.scut.p2ploanplatform.utils.AutoTrigger;
import com.scut.p2ploanplatform.utils.AutowireField;
import com.scut.p2ploanplatform.utils.ThirdPartyOperationInterface;
import com.scut.p2ploanplatform.vo.RepayExecutionResultVo;
import com.scut.p2ploanplatform.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class RepayExecutionServiceImpl implements RepayExecutionService {
    private static final String NOTICE_TITLE = "还款通知";
    private boolean isColdStart = true;

    @AutowireField
    private PurchaseService purchaseService;
    @AutowireField
    private NoticeService noticeService;
    @AutowireField
    private WaterBillService waterBillService;
    @AutowireField
    private UserService userService;
    @AutowireField
    private RepayPlanDao repayPlanDao;
    @AutowireField
    private RepayService repayService;
    @AutowireField
    private GuarantorService guarantorService;
    @AutowireField
    private CreditService creditService;

    @Autowired
    public void setRepayPlanDao(RepayPlanDao repayPlanDao) {
        this.repayPlanDao = repayPlanDao;
    }

    @Autowired
    public void setPurchaseService(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Autowired
    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setWaterBillService(WaterBillService waterBillService) {
        this.waterBillService = waterBillService;
    }

    @Autowired
    public void setRepayService(RepayService repayService) {
        this.repayService = repayService;
    }

    @Autowired
    public void setGuarantorService(GuarantorService guarantorService) {
        this.guarantorService = guarantorService;
    }

    @Autowired
    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    public RepayExecutionServiceImpl() throws Exception {
        new AutoTrigger(getClass().getDeclaredMethod("doRepay"), this, 1, 0, 0, true);
    }

    private <T extends Serializable> T deepCopy(T object) {
        return SerializationUtils.clone(object);
    }

    @Override
    public synchronized List<RepayExecutionResultVo> doRepay() {
        log.info("Starting daily repay process");
        List<RepayExecutionResultVo> results = new LinkedList<>();

        if (isColdStart) {
            isColdStart = false;
            log.debug("Updating plan status (from cold start)");
            int rowsAffected = repayPlanDao.updatePlanStatus();
            log.debug("Completed query, " + rowsAffected + " rows affected");
        }

        log.debug("Querying unpaid plans");
        List<RepayPlan> plans = repayPlanDao.findAllUnpaidPlan();
        log.debug("Completed query, got " + plans.size() + " results");

        if ("".equals(ThirdPartyOperationInterface.getApiKey())) {
            log.debug("Updating third party api key");
            ThirdPartyOperationInterface.setApiKey("f516d5a5-8380-4bd3-92d1-aff612b377a2");
        }

        for (RepayPlan plan : plans) {
            // 还款流程
            try {
                RepayExecutionResultVo result = new RepayExecutionResultVo();
                result.setRepayPlanOld(deepCopy(plan));
                // 获取认购信息
                Purchase purchase = purchaseService.showPurchaseById(plan.getPurchaseId());

                // 获取用户信息
                User borrower = userService.findUser(purchase.getBorrowerId());
                User investor = userService.findUser(purchase.getInvestorId());
                Guarantor guarantor = guarantorService.findGuarantor(purchase.getGuarantorId());

                // 转账
                ResultVo transferResult;
                // 已由担保人代付，还款默认转到担保人账号，其余情况都转到投资者账号
//                User payee = (plan.getStatus() == RepayPlanStatus.GUARANTOR_PAID_ADVANCE.getStatus()) ? guarantor : investor;
//                User payer = borrower;
                String borrowerMessage = null, guarantorMessage = null, investorMessage = null;
                boolean toGuarantor = false;

                if (plan.getStatus().equals(RepayPlanStatus.GUARANTOR_PAID_ADVANCE.getStatus())) {
                    transferResult = ThirdPartyOperationInterface.transfer(borrower.getThirdPartyId(), guarantor.getThirdPartyId(), plan.getAmount());
                    toGuarantor = true;
                }
                else
                    transferResult = ThirdPartyOperationInterface.transfer(borrower.getThirdPartyId(), investor.getThirdPartyId(), plan.getAmount());

                result.setBorrowerTransferResult(transferResult);

                if (transferResult.getCode() == 0) {
                    // 贷款人还款成功：贷款人 -> 投资者/担保人
                    plan.setRealRepayDate(new Date(new Date().getTime() / 86400000 * 86400000));
                    borrowerMessage = String.format("尊敬的 %s 用户，您本月贷款还款成功，已成功还款 %s 元", borrower.getName(), plan.getAmount().toString());
                    if (toGuarantor) {
                        plan.setStatus(RepayPlanStatus.OVERDUE_SUCCEEDED.getStatus());
                        waterBillService.addRepayRecord(plan.getPlanId(), plan.getPurchaseId(), borrower.getUserId(), guarantor.getGuarantorId(), plan.getRealRepayDate(), plan.getAmount());
                        guarantorMessage = String.format("尊敬的 %s 用户，您担保的贷款人 %s 本月已完成还款，还款金额已经转入您的第三方账号中", guarantor.getName(), borrower.getName());
                        purchaseService.overdueToSubscribed(plan.getPurchaseId());
                    } else {
                        plan.setStatus((plan.getStatus().equals(RepayPlanStatus.SCHEDULED.getStatus())) ? RepayPlanStatus.SUCCEEDED.getStatus() : RepayPlanStatus.OVERDUE_SUCCEEDED.getStatus());
                        waterBillService.addRepayRecord(plan.getPlanId(), plan.getPurchaseId(), borrower.getUserId(), investor.getUserId(), plan.getRealRepayDate(), plan.getAmount());
                        investorMessage = String.format("尊敬的 %s 用户，您本月投资的贷款已完成还款，已成功收款 %s 元", investor.getName(), plan.getAmount().toString());
                    }
                } else {
                    if (!plan.getStatus().equals(RepayPlanStatus.GUARANTOR_PAID_ADVANCE.getStatus())) {
                        // calling PurchaseService: set overdue status
                        purchaseService.purchaseOverdue(plan.getPurchaseId());
                        // 贷款人还款失败，执行垫付：担保人 -> 投资者
                        transferResult = ThirdPartyOperationInterface.transfer(guarantor.getThirdPartyId(), investor.getThirdPartyId(), plan.getAmount());
                        result.setGuarantorTransferResult(transferResult);
                        borrowerMessage = String.format("尊敬的 %s 用户，您的贷款本月还款失败，请检查第三方账号中金额，确保在系统重试还款前，拥有足够金额进行划扣", borrower.getName());

                        if (transferResult.getCode() == 0) {
                            // 担保人代付成功
                            plan.setStatus(RepayPlanStatus.GUARANTOR_PAID_ADVANCE.getStatus());
                            waterBillService.addRepayRecord(plan.getPlanId(), plan.getPurchaseId(), guarantor.getGuarantorId(), investor.getUserId(), new Date(new Date().getTime() / 86400000 * 86400000), plan.getAmount());
                            guarantorMessage = String.format("尊敬的 %s 用户，您担保的贷款人 %s 未能及时还款，系统已执行担保人垫付操作", guarantor.getName(), borrower.getName());
                            investorMessage = String.format("尊敬的 %s 用户，您投资的贷款已完成本月还款，已成功收款 %s 元", borrower.getName(), plan.getAmount().toString());
                        } else {
                            guarantorMessage = String.format("尊敬的 %s 用户，您担保的贷款人 %s 未能及时还款，系统尝试进行还款但未能成功，原因为： %s，请确保第三方账号拥有足够的金额", guarantor.getName(), borrower.getName(), transferResult.getMsg());
                            // 支付失败，投资方money全部木大
                            // 只对首次失败通知投资方
                            if (!plan.getStatus().equals(RepayPlanStatus.OVERDUE.getStatus())) {
                                investorMessage = String.format("尊敬的 %s 用户，您投资贷款本月未及时还款，系统已经为您执行逾期流程", investor.getName());
                                plan.setStatus(RepayPlanStatus.OVERDUE.getStatus());
                            }
                        }
                    }

                    // 28天后的逾期流程
                    if ((new Date().getTime() / 86400000) - (plan.getRepayDate().getTime() / 86400000) >= 28 && !plan.isOverdueProceeded()) {
                        plan.setOverdueProceeded(true);
                        // todo: unit test
                        Integer creditScore = creditService.getCreditScore(borrower.getUserId());
                        Integer reducedScore = Math.max(0, creditScore - 10);
                        creditService.updateCreditScore(borrower.getUserId(), reducedScore);
                    }
                }
                repayService.updateRepayPlan(plan.getPlanId(), RepayPlanStatus.values()[plan.getStatus()], plan.getRealRepayDate());

                if (borrowerMessage != null)
                    result.setBorrowerNotice(noticeService.sendNotice(borrower.getUserId(), NOTICE_TITLE, borrowerMessage));
                if (guarantorMessage != null)
                    result.setGuarantorNotice(noticeService.sendNotice(guarantor.getGuarantorId(), NOTICE_TITLE, guarantorMessage));
                if (investorMessage != null)
                    result.setInvestorNotice(noticeService.sendNotice(investor.getUserId(), NOTICE_TITLE, investorMessage));
                result.setRepayPlanNew(deepCopy(plan));

                results.add(result);
            } catch (Exception e) {
                log.error("Exception caught while executing repay routine in plan id: " + plan.getPlanId(), e);
            }

        }
        return results;
    }
}
