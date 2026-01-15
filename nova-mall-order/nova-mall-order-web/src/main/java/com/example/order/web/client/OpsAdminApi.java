package com.example.order.web.client;

import com.example.common.dto.Result;
import com.example.order.web.client.dto.OpsActivityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "ops-service",
        url = "${service.ops.base-url:http://localhost:8090}",
        path = "/admin",
        configuration = OpsFeignConfig.class)
public interface OpsAdminApi {

    @GetMapping("/activities/active")
    Result<List<OpsActivityDTO>> listActive();
}

