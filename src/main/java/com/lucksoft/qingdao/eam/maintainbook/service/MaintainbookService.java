package com.lucksoft.qingdao.eam.maintainbook.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.eam.maintainbook.dto.MaintainbookCriteriaDTO;
import com.lucksoft.qingdao.eam.maintainbook.dto.PageResultDTO;
import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbook;
import com.lucksoft.qingdao.eam.maintainbook.mapper.MaintainbookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Maintainbook 的业务逻辑服务层.
 */
@Service
@Transactional
public class MaintainbookService {

    @Autowired
    private MaintainbookMapper maintainbookMapper;

    public Maintainbook save(Maintainbook maintainbook) {
        if (maintainbook.getIndocno() == null) {
            maintainbookMapper.insert(maintainbook);
        } else {
            maintainbookMapper.update(maintainbook);
        }
        return maintainbook;
    }

    @Transactional(readOnly = true)
    public PageResultDTO<Maintainbook> findByCriteria(MaintainbookCriteriaDTO criteria, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Maintainbook> list = maintainbookMapper.findByCriteria(criteria);
        PageInfo<Maintainbook> pageInfo = new PageInfo<>(list);

        PageResultDTO<Maintainbook> pageResult = new PageResultDTO<>();
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setPages(pageInfo.getPages());
        pageResult.setList(pageInfo.getList());

        return pageResult;
    }

    @Transactional(readOnly = true)
    public Optional<Maintainbook> findOne(BigDecimal id) {
        return Optional.ofNullable(maintainbookMapper.findById(id));
    }

    public void delete(BigDecimal id) {
        maintainbookMapper.deleteById(id);
    }

    //<editor-fold defaultstate="collapsed" desc="TODO: 手动迁移业务逻辑指南">
    /*
     * =================================================================================
     * ======================== !!! 重要：请手动迁移以下业务逻辑 !!! ========================
     * =================================================================================
     */
    //</editor-fold>
}
