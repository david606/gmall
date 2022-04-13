package org.sirius.gmall.search.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 上午10:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private Date createTime;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
