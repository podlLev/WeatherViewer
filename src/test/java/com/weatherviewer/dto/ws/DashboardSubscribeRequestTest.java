package com.weatherviewer.dto.ws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardSubscribeRequestTest {

    @Test
    void gettersAndSetters_roundTripValues() {
        DashboardSubscribeRequest request = new DashboardSubscribeRequest();
        request.setSort("nameAsc");
        request.setPage(2);

        assertThat(request.getSort()).isEqualTo("nameAsc");
        assertThat(request.getPage()).isEqualTo(2);
    }

    @Test
    void newInstance_hasNullSortAndPage() {
        DashboardSubscribeRequest request = new DashboardSubscribeRequest();

        assertThat(request.getSort()).isNull();
        assertThat(request.getPage()).isNull();
    }

}
