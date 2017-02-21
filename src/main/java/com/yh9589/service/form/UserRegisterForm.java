package com.yh9589.service.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Size;

/**
 * Created by beryh on 2017-02-21.
 */
@Data
@NoArgsConstructor
public class UserRegisterForm {
    @NonNull
    @Size(min=4, max=16, message="{username.size}")
    private String username;

    @NonNull
    @Size(min=4, max=16, message="{password.size}")
    private String password;

    @Size(min=4, max=16, message="{password.size}")
    private String passwordConfirm;

    public boolean checkPasswordValidation() {
        return password.equals(passwordConfirm);
    }

}
