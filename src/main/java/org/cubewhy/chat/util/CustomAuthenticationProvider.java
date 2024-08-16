package org.cubewhy.chat.util;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.Serializable;

public class CustomAuthenticationProvider implements AuthenticationProvider, Serializable {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(authentication.getPrincipal(),
//                authentication.getCredentials(), ));
//        result.setDetails(authentication.getDetails());
        return authentication;
        // todo
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
