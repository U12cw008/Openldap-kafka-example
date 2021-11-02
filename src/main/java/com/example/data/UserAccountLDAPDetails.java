package com.example.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccountLDAPDetails {
    private String username;
    private String userId;
    private String dn;
    private String firstname;
    private String surname;


}
