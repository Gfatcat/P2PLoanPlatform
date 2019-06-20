package com.scut.p2ploanplatform.dao;

import com.scut.p2ploanplatform.entity.Guarantor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * @author: Light
 * @date: 2019/6/19 10:54
 * @description:
 */
public interface GuarantorDao {

    @Insert("INSERT INTO `p2p`.`guarantor` (`guarantor_id`, `authority_amount`) VALUES (#{guarantorId}, #{guarantorId}")
    int insertGuarantor(Guarantor guarantor);

    @Update("UPDATE `p2p`.`guarantor` SET `authority_amount` = #{authorityAmount} WHERE `guarantor_id` = #{guarantorId}")
    int updateGuarantor(Guarantor guarantor);

    @Select("SELECT * FROM `p2p`.`guarantor` WHERE guarantor_id = #{guarantorId}")
    Guarantor selectGuarantor(String guarantorId);

    @Delete("DELETE FROM `p2p`.`guarantor` WHERE guarantor_id = #{guarantorId}")
    int deleteGuarantor(String guarantorId);

}