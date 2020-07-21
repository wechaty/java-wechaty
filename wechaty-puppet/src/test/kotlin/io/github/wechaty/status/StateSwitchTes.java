package io.github.wechaty.status;

import io.github.wechaty.StateEnum;
import org.junit.Test;

/**
 * @ClassName : StateSwitchTest
 * @Description :
 * @Author : cybersa
 * @Date: 2020-07-19 14:52
 */
public class StateSwitchTes {

    @Test
    public void test() {
        StateSwitch stateSwitch = new StateSwitch();
        stateSwitch.addEventListener(StateEnum.ON, (a) -> {
            System.out.println(a);
        });
    }

}
