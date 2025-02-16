package com.ylz.yx.pay.system.log;

import com.ylz.yx.pay.system.log.dto.GetOperLogDTO;

import java.util.Map;

public interface OperLogService {

    Map<String, Object> getRzjlList(GetOperLogDTO getOperLogDTO) throws Exception;

    Map<String, Object> getRzjlbw(GetOperLogDTO getOperLogDTO);
}
