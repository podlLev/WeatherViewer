package com.weatherviewer.dto.ws;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sent by the client (STOMP SEND to {@code /app/dashboard.subscribe}) after connecting, or whenever it re-sorts/re-pages the dashboard in place. */
@Getter
@Setter
@NoArgsConstructor
public class DashboardSubscribeRequest {

    private String sort;
    private Integer page;

}
