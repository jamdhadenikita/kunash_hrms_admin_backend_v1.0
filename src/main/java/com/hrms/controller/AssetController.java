package com.hrms.controller;

import com.hrms.dto.request.AssetRequestDTO;
import com.hrms.dto.response.AssetResponseDTO;
import com.hrms.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    // ========== ASSET CRUD ==========

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAsset(@RequestBody AssetRequestDTO dto) {
        try {
            AssetResponseDTO asset = assetService.createAsset(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset created successfully");
            response.put("data", asset);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateAsset(@PathVariable Long id, @RequestBody AssetRequestDTO dto) {
        try {
            AssetResponseDTO asset = assetService.updateAsset(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset updated successfully");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<Map<String, Object>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssetResponseDTO> assets = assetService.getAllAssets(
                PageRequest.of(page, size, Sort.by("id").descending()));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", assets.getContent());
        response.put("totalPages", assets.getTotalPages());
        response.put("totalElements", assets.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<Map<String, Object>> getAssetById(@PathVariable Long id) {
        AssetResponseDTO asset = assetService.getAssetById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", asset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-asset-id/{assetId}")
    public ResponseEntity<Map<String, Object>> getAssetByAssetId(@PathVariable String assetId) {
        AssetResponseDTO asset = assetService.getAssetByAssetId(assetId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", asset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAssets(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssetResponseDTO> assets = assetService.searchAssets(keyword, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", assets.getContent());
        response.put("totalElements", assets.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Asset deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ========== ASSIGNMENT ==========

    @PostMapping("/assign/{assetId}")
    public ResponseEntity<Map<String, Object>> assignAsset(@PathVariable Long assetId, @RequestBody AssetRequestDTO dto) {
        try {
            AssetResponseDTO asset = assetService.assignAsset(assetId, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset assigned successfully");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/return/{assetId}")
    public ResponseEntity<Map<String, Object>> returnAsset(
            @PathVariable Long assetId,
            @RequestParam String conditionAtReturn) {
        try {
            AssetResponseDTO asset = assetService.returnAsset(assetId, conditionAtReturn);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset returned successfully");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== REQUESTS ==========

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> createAssetRequest(@RequestBody AssetRequestDTO dto) {
        try {
            AssetResponseDTO asset = assetService.createAssetRequest(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset request submitted successfully");
            response.put("data", asset);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/approve-request/{assetId}")
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long assetId,
            @RequestParam String approvedBy) {
        try {
            AssetResponseDTO asset = assetService.approveRequest(assetId, approvedBy);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request approved successfully");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/reject-request/{assetId}")
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @PathVariable Long assetId,
            @RequestParam String remarks) {
        try {
            AssetResponseDTO asset = assetService.rejectRequest(assetId, remarks);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request rejected");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/get-pending-requests")
    public ResponseEntity<Map<String, Object>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssetResponseDTO> requests = assetService.getPendingRequests(PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", requests.getContent());
        response.put("totalElements", requests.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ========== MAINTENANCE ==========

    @PostMapping("/maintenance/{assetId}")
    public ResponseEntity<Map<String, Object>> addMaintenanceRecord(
            @PathVariable Long assetId,
            @RequestBody AssetRequestDTO dto) {
        try {
            AssetResponseDTO asset = assetService.addMaintenanceRecord(assetId, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Maintenance record added");
            response.put("data", asset);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/maintenance/update-status/{maintenanceId}")
    public ResponseEntity<Map<String, Object>> updateMaintenanceStatus(
            @PathVariable Long maintenanceId,
            @RequestParam String status) {
        try {
            AssetResponseDTO asset = assetService.updateMaintenanceStatus(maintenanceId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Maintenance status updated");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== DASHBOARD STATS ==========

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAssetStats() {
        Map<String, Long> stats = assetService.getAssetStats();
        Map<String, Long> categoryStats = assetService.getCategoryStats();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        response.put("categoryStats", categoryStats);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-total-count")
    public ResponseEntity<Map<String, Object>> getTotalCount() {
        long count = assetService.getTotalCount();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalAssets", count);
        return ResponseEntity.ok(response);
    }

    // ========== FILTERS ==========

    @GetMapping("/filter-by-status/{status}")
    public ResponseEntity<Map<String, Object>> getAssetsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssetResponseDTO> assets = assetService.getAllAssets(PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", assets.getContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/maintenance/{assetId}")
    public ResponseEntity<Map<String, Object>> deleteMaintenanceRecord(@PathVariable Long assetId) {
        try {
            AssetResponseDTO asset = assetService.deleteMaintenanceRecord(assetId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Maintenance record deleted successfully");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/complete-maintenance/{assetId}")
    public ResponseEntity<Map<String, Object>> completeMaintenance(@PathVariable Long assetId) {
        try {
            AssetResponseDTO asset = assetService.completeMaintenance(assetId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Maintenance completed, asset is now available");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/filter-by-category/{category}")
    public ResponseEntity<Map<String, Object>> getAssetsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssetResponseDTO> assets = assetService.getAllAssets(PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", assets.getContent());
        return ResponseEntity.ok(response);
    }
}
