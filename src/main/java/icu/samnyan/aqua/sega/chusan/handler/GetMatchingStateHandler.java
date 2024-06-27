package icu.samnyan.aqua.sega.chusan.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import icu.samnyan.aqua.sega.general.BaseHandler;
import icu.samnyan.aqua.sega.chusan.model.response.data.MatchingMemberInfo;
import icu.samnyan.aqua.sega.chusan.model.response.data.MatchingWaitState;
import icu.samnyan.aqua.sega.util.jackson.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Component("ChusanGetMatchingStateHandler")
public class GetMatchingStateHandler implements BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetMatchingStateHandler.class);

    private final StringMapper mapper;

    @Autowired
    public GetMatchingStateHandler(StringMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String handle(Map<String, Object> request) throws JsonProcessingException {
        String roomId = (String) request.get("roomId");
        MatchingMemberInfo matchingMemberInfo = mapper.convert(request.get("matchingMemberInfo"), MatchingMemberInfo.class);

        Map<String, Object> resultMap = new LinkedHashMap<>();

        MatchingWaitState matchingWaitState = new MatchingWaitState();
        matchingWaitState.setFinish(true);

        resultMap.put("matchingWaitState", matchingWaitState);

        String json = mapper.write(resultMap);
        logger.info("Response: " + json);
        return json;
    }

}
