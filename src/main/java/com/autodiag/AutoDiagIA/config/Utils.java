package com.autodiag.AutoDiagIA.config;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.entity.Log;
import com.autodiag.AutoDiagIA.entity.Session;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Utils {

@Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 1. Mapeamento de Entity -> DTO (Achata o TriggerConfig e ajusta o endReason)
        modelMapper.addMappings(new PropertyMap<Session, SessionDTO>() {
            @Override
            protected void configure() {

                // Entity -> DTO
                map().getTriggerConfig().setPid(source.getTriggerPid());
                map().getTriggerConfig().setOperator(source.getTriggerOperator());
                map().getTriggerConfig().setTriggerValue(source.getTriggerValue());
                map().getTriggerConfig().setValueIndex(source.getTriggerValueIndex());
            }
        });

        // DTO -> Entity
        modelMapper.addMappings(new PropertyMap<SessionDTO, Session>() {
            @Override
            protected void configure() {
                map().setTriggerPid(source.getTriggerConfig().getPid());
                map().setTriggerOperator(source.getTriggerConfig().getOperator());
                map().setTriggerValue(source.getTriggerConfig().getTriggerValue());
                map().setTriggerValueIndex(source.getTriggerConfig().getValueIndex());
            }
        });

        modelMapper.addMappings(new PropertyMap<Log, LogDTO>() {

            @Override
            protected void configure() {
                map().setSessionId(source.getSession().getId());
            }


        });

        return modelMapper;
    }
}