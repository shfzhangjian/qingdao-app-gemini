package com.lucksoft.qingdao.eam.maintainbook.controller;

import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbookdt;
import com.lucksoft.qingdao.eam.maintainbook.service.MaintainbookdtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Maintainbookdt 的REST控制器.
 */
@RestController
@RequestMapping("/api/maintainbookdt")
public class MaintainbookdtController {

    @Autowired
    private MaintainbookdtService maintainbookdtService;

    @PostMapping("/save")
    public ResponseEntity<Maintainbookdt> save(@RequestBody Maintainbookdt maintainbookdt) {
        Maintainbookdt result = maintainbookdtService.save(maintainbookdt);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maintainbookdt> getById(@PathVariable BigDecimal id) {
        return maintainbookdtService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable BigDecimal id) {
        maintainbookdtService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
