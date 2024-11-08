package com.erikssonherlo.incident.infrastructure.adapter.input;

import com.erikssonherlo.common.application.response.ApiResponse;
import com.erikssonherlo.common.application.response.PaginatedResponse;
import com.erikssonherlo.common.infraestructure.security.anotation.ValidateRole;
import com.erikssonherlo.incident.application.dto.CreateIncidentDTO;
import com.erikssonherlo.incident.application.dto.UpdateIncidentDTO;
import com.erikssonherlo.incident.domain.model.Incident;
import com.erikssonherlo.incident.infrastructure.port.input.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final FindIncidentInputPort findIncidentInputPort;
    private final DeleteIncidentInputPort deleteIncidentInputPort;
    private final CreateIncidentInputPort createIncidentInputPort;
    private final UpdateIncidentInputPort updateIncidentInputPort;
    private final GetAllIncidentsInputPort getAllIncidentsInputPort;
    private final GetAllIncidentsByStatusInputPort getAllIncidentsByStatusInputPort;
    private final ReportAllIncidentsInputPort reportAllIncidentsInputPort;

    @ValidateRole({"ADMINISTRATOR", "STORE", "WAREHOUSE"})
    @GetMapping()
    public PaginatedResponse<List<Incident>> getAllIncidents(
            @PageableDefault(page = 0,size = 10) Pageable pageable,
            @RequestParam(value = "storeId", required = false) List<Long> storeIds,
            @RequestParam(value = "status", required = false) String status

    ){
        Page<Incident> incidentsPage = getAllIncidentsInputPort.getAllIncidents(pageable, storeIds, status);
        return new PaginatedResponse<>(HttpStatus.OK.value(),"SUCCESS", HttpStatus.OK,incidentsPage.getContent(),incidentsPage.getPageable(),incidentsPage.isLast(),incidentsPage.isFirst(),incidentsPage.hasNext(),incidentsPage.hasPrevious(),incidentsPage.getTotalPages(),(int) incidentsPage.getTotalElements());
    }

    @ValidateRole({"ADMINISTRATOR", "STORE", "WAREHOUSE"})
    @GetMapping("/status/{status}")
    public PaginatedResponse<List<Incident>> getAllIncidentsByStatus(@PathVariable String status, @PageableDefault(page = 0,size = 10) Pageable pageable){
        Page<Incident> incidentsPage = getAllIncidentsByStatusInputPort.getAllIncidentsByStatus(status,pageable);
        return new PaginatedResponse<>(HttpStatus.OK.value(),"SUCCESS", HttpStatus.OK,incidentsPage.getContent(),incidentsPage.getPageable(),incidentsPage.isLast(),incidentsPage.isFirst(),incidentsPage.hasNext(),incidentsPage.hasPrevious(),incidentsPage.getTotalPages(),(int) incidentsPage.getTotalElements());
    }

    @ValidateRole({"ADMINISTRATOR", "STORE", "WAREHOUSE"})
    @GetMapping("/{id}")
    public ApiResponse<Incident> findIncident(@PathVariable Long id){
        Incident incident = findIncidentInputPort.findIncident(id);
        return new ApiResponse<>(HttpStatus.OK.value(),"SUCCESS", HttpStatus.OK,incident);
    }

    @ValidateRole({"ADMINISTRATOR", "STORE"})
    @PostMapping()
    public ApiResponse<Incident> createIncident(@RequestBody @Valid CreateIncidentDTO createIncidentDTO){
        return new ApiResponse<>(HttpStatus.CREATED.value(),"SUCCESS",HttpStatus.CREATED,createIncidentInputPort.createIncident(createIncidentDTO));
    }

    @ValidateRole({"ADMINISTRATOR", "STORE", "WAREHOUSE"})
    @PutMapping("/{id}")
    public ApiResponse<Incident> updateIncident(@PathVariable Long id, @RequestBody @Valid UpdateIncidentDTO updateIncidentDTO){
        return new ApiResponse<>(HttpStatus.OK.value(),"SUCCESS",HttpStatus.OK, updateIncidentInputPort.updateIncident(id,updateIncidentDTO));
    }

    @ValidateRole({"ADMINISTRATOR"})
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteIncident(@PathVariable Long id){
        return new ApiResponse<>(HttpStatus.NO_CONTENT.value(),"SUCCESS",HttpStatus.NO_CONTENT,deleteIncidentInputPort.deleteIncident(id));
    }

    @ValidateRole({"ADMINISTRATOR", "STORE", "WAREHOUSE"})
    @GetMapping("/report/getAllIncidents")
    public ApiResponse<List<Incident>> reportAllIncidents(@RequestParam(value = "storeId", required = false) List<Long> storeIds,
                                                          @RequestParam(value = "status", required = false) String status,
                                                          @RequestParam(value = "startDate", required = false) String startDate,
                                                          @RequestParam(value = "endDate", required = false) String endDate){
        return new ApiResponse<>(HttpStatus.OK.value(),"SUCCESS",HttpStatus.OK, reportAllIncidentsInputPort.reportAllIncidents(storeIds,status,startDate,endDate));
    }
}
