package icu.samnyan.aqua.sega.maimai2.handler;

import com.fasterxml.jackson.core.JsonProcessingException;

import icu.samnyan.aqua.sega.maimai2.model.Mai2UserCardRepo;
import icu.samnyan.aqua.sega.maimai2.model.Mai2UserDataRepo;
import icu.samnyan.aqua.sega.maimai2.model.Mai2UserPrintDetailRepo;
import icu.samnyan.aqua.sega.general.BaseHandler;
import icu.samnyan.aqua.sega.maimai2.model.request.UpsertUserPrint;
import icu.samnyan.aqua.sega.maimai2.model.userdata.Mai2UserCard;
import icu.samnyan.aqua.sega.maimai2.model.userdata.Mai2UserDetail;
import icu.samnyan.aqua.sega.maimai2.model.userdata.Mai2UserPrintDetail;
import icu.samnyan.aqua.sega.util.jackson.BasicMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Component("Maimai2UpsertUserPrintHandler")
@AllArgsConstructor
public class UpsertUserPrintHandler implements BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpsertUserPrintHandler.class);
    private final BasicMapper mapper;

    private final Mai2UserCardRepo userCardRepository;
    private final Mai2UserPrintDetailRepo userPrintDetailRepository;
    private final Mai2UserDataRepo userDataRepository;

    @Override
    public String handle(Map<String, Object> request) throws JsonProcessingException {
        long userId = ((Number) request.get("userId")).longValue();

        Mai2UserDetail userData;

        Optional<Mai2UserDetail> userOptional = userDataRepository.findByCardExtId(userId);
        if (userOptional.isPresent()) {
            userData = userOptional.get();
        } else {
            logger.error("User not found. userId: {}", userId);
            return null;
        }

        UpsertUserPrint upsertUserPrint = mapper.convert(request, UpsertUserPrint.class);

        Mai2UserPrintDetail userPrintDetail = upsertUserPrint.getUserPrintDetail();
        Mai2UserCard newUserCard = userPrintDetail.getUserCard();

        newUserCard.setUser(userData);
        userPrintDetail.setUser(userData);

        newUserCard.setStartDate("2019-01-01 00:00:00.000000");
        newUserCard.setEndDate("2029-01-01 00:00:00.000000");
        userPrintDetail.setSerialId("FAKECARDIMAG12345678");

        Optional<Mai2UserCard> userCardOptional = userCardRepository.findByUserAndCardId(newUserCard.getUser(), newUserCard.getCardId());
        if (userCardOptional.isPresent()) {
            Mai2UserCard userCard = userCardOptional.get();
            newUserCard.setId(userCard.getId());
        }

        userCardRepository.save(newUserCard);
        userPrintDetailRepository.save(userPrintDetail);

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("returnCode", 1);
        resultMap.put("orderId", 0);
        resultMap.put("serialId", "FAKECARDIMAG12345678");
        resultMap.put("startDate", "2019-01-01 00:00:00.000000");
        resultMap.put("endDate", "2029-01-01 00:00:00.000000");

        return mapper.write(resultMap);
    }
}
