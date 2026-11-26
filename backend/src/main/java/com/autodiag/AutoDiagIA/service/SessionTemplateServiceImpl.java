package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.session.SessionTemplateDTO;
import com.autodiag.AutoDiagIA.dto.session.SessionTemplateRequest;
import com.autodiag.AutoDiagIA.entity.SessionTemplate;
import com.autodiag.AutoDiagIA.exception.InvalidRequestException;
import com.autodiag.AutoDiagIA.exception.ResourceNotFoundException;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.repository.SessionTemplateRepository;
import com.autodiag.AutoDiagIA.session.TriggerConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class SessionTemplateServiceImpl implements SessionTemplateService {

    private final SessionTemplateRepository repository;

    public SessionTemplateServiceImpl(SessionTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<SessionTemplateDTO> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SessionTemplateDTO create(SessionTemplateRequest request) {
        validate(request);
        return toDto(repository.save(apply(new SessionTemplate(), request)));
    }

    @Override
    @Transactional
    public SessionTemplateDTO update(Long id, SessionTemplateRequest request) {
        validate(request);
        SessionTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modelo de sessão de id: " + id + " inexistente"
                ));
        return toDto(repository.save(apply(template, request)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SessionTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modelo de sessão de id: " + id + " inexistente"
                ));
        repository.delete(template);
    }

    private void validate(SessionTemplateRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Os dados do modelo não foram informados.");
        }
        if (request.name() == null || request.name().isBlank()
                || request.name().trim().length() > 120) {
            throw new InvalidRequestException(
                    "Informe um nome para o modelo com até 120 caracteres."
            );
        }
        if (request.startReason() == null || request.startReason().isBlank()
                || request.startReason().trim().length() > 1000) {
            throw new InvalidRequestException(
                    "Informe o objetivo do modelo com até 1000 caracteres."
            );
        }
        if (request.pids() == null || request.pids().isEmpty()) {
            throw new InvalidRequestException("Selecione pelo menos um PID para o modelo.");
        }
        request.pids().forEach(this::toPidCode);

        if (request.includeSessionSettings()) {
            if (request.intervalMs() == null || request.intervalMs() <= 0
                    || request.maxDurationMs() == null || request.maxDurationMs() <= 0) {
                throw new InvalidRequestException(
                        "Intervalo e duração máxima do modelo devem ser positivos."
                );
            }
            validateTrigger(request.trigger());
        } else if (request.intervalMs() != null || request.maxDurationMs() != null
                || request.trigger() != null) {
            throw new InvalidRequestException(
                    "Um modelo simples não pode incluir configurações de coleta."
            );
        }
    }

    private void validateTrigger(TriggerConfig trigger) {
        if (trigger == null) {
            return;
        }
        if (trigger.getPid() == null || trigger.getPid().isBlank()
                || trigger.getValueIndex() == null || trigger.getValueIndex() < 0
                || trigger.getOperator() == null || trigger.getTriggerValue() == null
                || !Double.isFinite(trigger.getTriggerValue())) {
            throw new InvalidRequestException("A configuração do trigger está incompleta.");
        }
        toPidCode(trigger.getPid());
    }

    private SessionTemplate apply(
            SessionTemplate template,
            SessionTemplateRequest request
    ) {
        template.setName(request.name().trim());
        template.setStartReason(request.startReason().trim());
        template.setPids(List.copyOf(new LinkedHashSet<>(
                request.pids().stream().map(this::toPidCode).toList()
        )));
        template.setIncludeSessionSettings(request.includeSessionSettings());
        template.setIntervalMs(request.includeSessionSettings() ? request.intervalMs() : null);
        template.setMaxDurationMs(
                request.includeSessionSettings() ? request.maxDurationMs() : null
        );

        TriggerConfig trigger = request.includeSessionSettings() ? request.trigger() : null;
        template.setTriggerPid(trigger == null ? null : toPidCode(trigger.getPid()));
        template.setTriggerOperator(trigger == null ? null : trigger.getOperator());
        template.setTriggerValue(trigger == null ? null : trigger.getTriggerValue());
        template.setTriggerValueIndex(trigger == null ? null : trigger.getValueIndex());
        return template;
    }

    private SessionTemplateDTO toDto(SessionTemplate template) {
        TriggerConfig trigger = template.getTriggerPid() == null
                ? null
                : new TriggerConfig(
                        template.getTriggerPid(),
                        template.getTriggerValueIndex(),
                        template.getTriggerOperator(),
                        template.getTriggerValue()
                );
        return new SessionTemplateDTO(
                template.getId(),
                template.getName(),
                template.getStartReason(),
                List.copyOf(template.getPids()),
                template.isIncludeSessionSettings(),
                template.getIntervalMs(),
                template.getMaxDurationMs(),
                trigger
        );
    }

    private String toPidCode(String pid) {
        try {
            return ObdPid.fromString(pid).getCode();
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "PID inválido no modelo: " + pid
            );
        }
    }
}
