package com.ihrm.social.dao;

import com.ihrm.domain.social_security.CityPaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 自定义dao接口继承
 *      JpaRepository<实体类，主键>
 *      JpaSpecificationExecutor<实体类>
 */
public interface CityPaymentItemDao extends JpaRepository<CityPaymentItem,String> ,JpaSpecificationExecutor<CityPaymentItem> {

    public List<CityPaymentItem> findAllByCityId(String id);
}
