package com.erikssonherlo.incident.application.usecase;

import com.erikssonherlo.common.application.exception.ResourceAlreadyExistsException;
import com.erikssonherlo.common.application.security.JWTService;
import com.erikssonherlo.incident.application.dto.CreateIncidentDTO;
import com.erikssonherlo.incident.application.dto.NotificationDto;
import com.erikssonherlo.incident.domain.model.Incident;
import com.erikssonherlo.incident.domain.model.IncidentDetail;
import com.erikssonherlo.incident.domain.model.enums.TemplateType;
import com.erikssonherlo.incident.infrastructure.port.input.CreateIncidentInputPort;
import com.erikssonherlo.incident.infrastructure.port.output.db.IncidentJpaRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateIncidentUseCase implements CreateIncidentInputPort {
    private final IncidentJpaRepositoryPort incidentJpaRepositoryPort;
    private final HttpServletRequest request;
    private final JWTService jwtService;
    private final NotificationDto notificationDto = new NotificationDto();
    private final RabbitTemplate rabbitTemplate;
    private final String queueName = "notifications";

    @Override
    public Incident createIncident(CreateIncidentDTO createIncidentDTO) {
        try {
            String token = jwtService.extractToken(request);
            Boolean existStore = true;
            Boolean existShipment = true;
            Boolean existUser = true;

            /*if (!existsStore(createIncidentDTO.storeId(), token)) {
                throw new ResourceNotFoundException("Store", "id", createIncidentDTO.storeId());
            }
            if (!existsShipment(createIncidentDTO.shipmentId(), token)) {
                throw new ResourceNotFoundException("Shipment", "id", createIncidentDTO.shipmentId());
            }
            if (!existsUser(createIncidentDTO.userEmail(), token)) {
                throw new ResourceNotFoundException("User", "email", createIncidentDTO.userEmail());
            }*/

                List<IncidentDetail> details = createIncidentDTO.details().stream()
                        .map(dto -> IncidentDetail.builder()
                                .productSku(dto.productSku())
                                .affectedQuantity(dto.affectedQuantity())
                                .reason(dto.reason())
                                .build())
                        .collect(Collectors.toList());

                Incident incident = Incident.builder()
                        .shipmentId(createIncidentDTO.shipmentId())
                        .storeId(createIncidentDTO.storeId())
                        .userEmail(createIncidentDTO.userEmail())
                        .status(createIncidentDTO.status())
                        .solution(createIncidentDTO.solution())
                        .details(details)
                        .build();

            notificationDto.setTo(createIncidentDTO.userEmail());
            notificationDto.setSubject("NEW INCIDENT CREATED");
            notificationDto.setTemplate(TemplateType.INCIDENT_CREATED);
            notificationDto.setMessage(Long.toString(incident.getIncidentId()));
            rabbitTemplate.convertAndSend(queueName, notificationDto);
            System.out.println("Email enviado a la cola: " + notificationDto);
            return incidentJpaRepositoryPort.saveIncident(incident);
        }catch (Exception e){
            throw e;
        }
    }
}
