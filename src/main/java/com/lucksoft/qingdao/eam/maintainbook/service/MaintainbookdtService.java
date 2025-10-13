package com.lucksoft.qingdao.eam.maintainbook.service;

import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbookdt;
import com.lucksoft.qingdao.eam.maintainbook.mapper.MaintainbookdtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Maintainbookdt 的业务逻辑服务层.
 */
@Service
@Transactional
public class MaintainbookdtService {

    @Autowired
    private MaintainbookdtMapper maintainbookdtMapper;

    public Maintainbookdt save(Maintainbookdt maintainbookdt) {
        if (maintainbookdt.getIndocno() == null) {
            maintainbookdtMapper.insert(maintainbookdt);
        } else {
            maintainbookdtMapper.update(maintainbookdt);
        }
        return maintainbookdt;
    }

    @Transactional(readOnly = true)
    public Optional<Maintainbookdt> findOne(BigDecimal id) {
        return Optional.ofNullable(maintainbookdtMapper.findById(id));
    }

    public void delete(BigDecimal id) {
        maintainbookdtMapper.deleteById(id);
    }

    //<editor-fold defaultstate="collapsed" desc="TODO: 手动迁移业务逻辑指南">
    /*
     * =================================================================================
     * ======================== !!! 重要：请手动迁移以下业务逻辑 !!! ========================
     * =================================================================================
     */
    //</editor-fold>
}
