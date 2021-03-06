package com.scut.p2ploanplatform.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.scut.p2ploanplatform.dao.AuthorityDao;
import com.scut.p2ploanplatform.dto.UserInfoGuarantorView;
import com.scut.p2ploanplatform.entity.CreditInfo;
import com.scut.p2ploanplatform.entity.Guarantor;
import com.scut.p2ploanplatform.entity.LoanApplication;
import com.scut.p2ploanplatform.entity.User;
import com.scut.p2ploanplatform.enums.NoticeStatusEnum;
import com.scut.p2ploanplatform.enums.ResultEnum;
import com.scut.p2ploanplatform.service.*;
import com.scut.p2ploanplatform.utils.ResultVoUtil;
import com.scut.p2ploanplatform.vo.PageVo;
import com.scut.p2ploanplatform.vo.ResultVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.scut.p2ploanplatform.utils.ResultVoUtil.error;

/**
 * @author: Light
 * @date: 2019/6/26 10:23
 * @description:
 */

@RestController
@RequestMapping("/guarantor")
public class GuarantorController {

    @Autowired
    UserService userService;
    @Autowired
    GuarantorService guarantorService;
    @Autowired
    LoanApplicationService applicationService;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    CreditService creditService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    AuthorityService authorityService;

    @RequestMapping("/guarantee/detail/{applicationId}")
    @GetMapping
    public ResultVo getDetail(@PathVariable Integer applicationId,
                              @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        LoanApplication application;
        try{
            application = applicationService.getApplicationById(applicationId);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }
        if(application == null){
            return error(ResultEnum.APPLICATION_NOT_EXIST);
        }
        return ResultVoUtil.success(application);
    }


    @RequestMapping("/guarantee/unreviewed")
    @GetMapping
    public ResultVo getUnreviewed(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
            @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getApplicationUnReviewed(pageNum, pageSize);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }

        if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
            return ResultVoUtil.error(ResultEnum.APPLICATION_NOT_EXIST);
        }

        return ResultVoUtil.success(new PageVo(
                applicationPageInfo.getPages(),
                applicationPageInfo.getTotal(),
                applicationPageInfo.getPageSize(),
                applicationPageInfo.getPageNum(),
                applicationPageInfo.getList()
        ));
    }

    @RequestMapping("/guarantee/review_passed")
    @GetMapping
    public ResultVo getReviewPassed(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                                  @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getApplicationReviewedPassed(pageNum, pageSize);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }

        if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
            return ResultVoUtil.error(ResultEnum.APPLICATION_NOT_EXIST);
        }

        return ResultVoUtil.success(new PageVo(
                applicationPageInfo.getPages(),
                applicationPageInfo.getTotal(),
                applicationPageInfo.getPageSize(),
                applicationPageInfo.getPageNum(),
                applicationPageInfo.getList()
        ));
    }

    @RequestMapping("/guarantee/review_rejected")
    @GetMapping
    public ResultVo getReviewRejected(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                                    @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getApplicationReviewedRejected(pageNum, pageSize);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }

        if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
            return ResultVoUtil.error(ResultEnum.APPLICATION_NOT_EXIST);
        }

        return ResultVoUtil.success(new PageVo(
                applicationPageInfo.getPages(),
                applicationPageInfo.getTotal(),
                applicationPageInfo.getPageSize(),
                applicationPageInfo.getPageNum(),
                applicationPageInfo.getList()
        ));
    }

    @RequestMapping("/guarantee/review_expired")
    @GetMapping
    public ResultVo getReviewExpired(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                                      @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getApplicationReviewExpired(pageNum, pageSize);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }

        if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
            return ResultVoUtil.error(ResultEnum.APPLICATION_NOT_EXIST);
        }

        return ResultVoUtil.success(new PageVo(
                applicationPageInfo.getPages(),
                applicationPageInfo.getTotal(),
                applicationPageInfo.getPageSize(),
                applicationPageInfo.getPageNum(),
                applicationPageInfo.getList()
        ));
    }

    @RequestMapping("/guarantee/all")
    @GetMapping
    public ResultVo getAllApplication(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                                      @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getAll012Application(pageNum, pageSize);
        }catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }

        if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
            return ResultVoUtil.error(ResultEnum.APPLICATION_NOT_EXIST);
        }

        return ResultVoUtil.success(new PageVo(
                applicationPageInfo.getPages(),
                applicationPageInfo.getTotal(),
                applicationPageInfo.getPageSize(),
                applicationPageInfo.getPageNum(),
                applicationPageInfo.getList()
        ));
    }

    @RequestMapping("/guarantee/pass")
    @GetMapping
    public ResultVo passApllication(@RequestParam("application_id") Integer applicationId,
                                      @SessionAttribute("user") String userId,
                                      @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        try {
            LoanApplication loanApplication = applicationService.getApplicationById(applicationId);
            if (loanApplication.getStatus()!=0) return ResultVoUtil.error(ResultEnum.REVIEW_PRIVILEGE_DENY);
            if (authorityService.getAuthorityAmount(guarantorService.findGuarantor(userId).getAuthorityId()).compareTo(loanApplication.getAmount())<0) return ResultVoUtil.error(ResultEnum.REVIEW_PRIVILEGE_DENY);
            if(applicationService.reviewPass(applicationId, userId)){
                noticeService.sendNotice(loanApplication.getBorrowerId(),"申请状态变化","您的申请被通过了");
                return ResultVoUtil.success();
            }
            else return ResultVoUtil.error(ResultEnum.REVIEW_PRIVILEGE_DENY);
        }
        catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }
    }

    @RequestMapping("/guarantee/reject")
    @GetMapping
    public ResultVo rejectApplication(@RequestParam("application_id") Integer applicationId,
                                      @SessionAttribute("user") String userId,
                                      @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }
        try {
            LoanApplication loanApplication = applicationService.getApplicationById(applicationId);
            if (loanApplication.getStatus()!=0) return ResultVoUtil.error(ResultEnum.REVIEW_PRIVILEGE_DENY);
            if (applicationService.reviewReject(applicationId, userId)){
                noticeService.sendNotice(loanApplication.getBorrowerId(),"申请状态变化","您的申请被拒绝了");
                return ResultVoUtil.success();
            }
            else return ResultVoUtil.error(ResultEnum.REVIEW_PRIVILEGE_DENY);
        }
        catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }
    }

    @RequestMapping("/guarantee/userInfo")
    @GetMapping
    public ResultVo getUserInfo(@RequestParam(value="user_id")String userId,
                                @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        try {
            User user = userService.findUser(userId);
            CreditInfo creditInfo= creditService.getCreditInfo(userId);
            if (user==null||creditInfo==null)
                return error(ResultEnum.USER_NOT_EXITST);
            UserInfoGuarantorView userInfoGuarantorView = new UserInfoGuarantorView(creditInfo,user);
            return ResultVoUtil.success(userInfoGuarantorView);
        }
        catch (Exception e)
        {
            return error(ResultEnum.PARAM_IS_INVALID);
        }
    }

    @RequestMapping("/guarantee/user_history")
    @GetMapping
    public ResultVo getUserHistory(@RequestParam(value="user_id")String userId,
                                   @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                                @SessionAttribute(value = "user_type") int userType){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }


        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = applicationService.getUserHistory(pageNum, pageSize, userId);
            if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
                return ResultVoUtil.success(applicationPageInfo);
            }
            return ResultVoUtil.success(applicationPageInfo);
        }
        catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }
    }

    @RequestMapping("/overdue")
    @GetMapping
    public ResultVo getOverdue(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
                               @SessionAttribute(value = "user_type") int userType,
                               @SessionAttribute(value = "user") String userId){
        if (userType != 2) {
            return error(ResultEnum.USER_AUTHORITY_DENY);
        }

        PageInfo applicationPageInfo;
        try{
            applicationPageInfo = purchaseService.getOverdueApplicationById(pageNum, pageSize, userId);
            if(applicationPageInfo == null||applicationPageInfo.getTotal()==0){
                return ResultVoUtil.success(applicationPageInfo);
            }
            return ResultVoUtil.success(applicationPageInfo);
        }
        catch (Exception e){
            return error(ResultEnum.PARAM_IS_INVALID);
        }
    }

}
