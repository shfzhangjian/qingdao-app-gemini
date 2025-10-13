package com.lucksoft.qingdao.eam.maintainbook.controller;

import com.lucksoft.qingdao.eam.maintainbook.dto.MaintainbookCriteriaDTO;
import com.lucksoft.qingdao.eam.maintainbook.dto.PageResultDTO;
import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbook;
import com.lucksoft.qingdao.eam.maintainbook.service.MaintainbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Maintainbook 的REST控制器.
 */
@RestController
@RequestMapping("/api/maintainbook")
public class MaintainbookController {

    @Autowired
    private MaintainbookService maintainbookService;

    @PostMapping("/save")
    public ResponseEntity<Maintainbook> save(@RequestBody Maintainbook maintainbook) {
        Maintainbook result = maintainbookService.save(maintainbook);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResultDTO<Maintainbook>> list(
            MaintainbookCriteriaDTO criteria,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResultDTO<Maintainbook> pageResult = maintainbookService.findByCriteria(criteria, pageNum, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maintainbook> getById(@PathVariable BigDecimal id) {
        return maintainbookService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable BigDecimal id) {
        maintainbookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
