package edufit_com_lms.module.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionRequest {
    private String content; // Ná»™i dung Ä‘Ã¡p Ã¡n (vd: Báº±ng 2)
    private Boolean isCorrect; // Pháº£i CÃ“ biáº¿n nÃ y Ä‘á»ƒ Admin Ä‘Ã¡nh dáº¥u cÃ¢u Ä‘Ãºng sai!
}
