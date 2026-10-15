package br.com.queue.controller.department;

import br.com.queue.dtos.department.ResponseDepartmentDto;
import br.com.queue.dtos.department.create.CreateDepartmentDto;
import br.com.queue.dtos.department.statistics.ResponseDepartmentDashBoardDto;
import br.com.queue.dtos.department.update.UpdateDepartmentDto;
import br.com.queue.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ResponseDepartmentDto> createDepartment(
            JwtAuthenticationToken token,
            @RequestBody @Valid CreateDepartmentDto dto) {

        var response = this.departmentService.createDepartment(token, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public ResponseEntity<ResponseDepartmentDto> updateDepartment(@RequestBody UpdateDepartmentDto dto) {

        var response = this.departmentService.updateDepartment(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<ResponseDepartmentDto> deleteDepartment(@PathVariable String departmentId) {

        var response = this.departmentService.deleteDepartment(departmentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseDepartmentDto>> getAllDepartments(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {

        var response = this.departmentService.getAllDepartments(token, page, size, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseDepartmentDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {

        var response = this.departmentService.getStatistics(token);
        return ResponseEntity.ok(response);
    }
}