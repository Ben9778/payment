package com.ylz.yx.pay.datasource;

import com.yilz.his.MdEncrypt.IMdEncryptText;
import com.yilz.his.MdEncrypt.MdEncryptText;
import com.ylz.svc.data.datasource.DefaultDataSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OracleDataSource extends DefaultDataSource {

    public static final String DRIVER = "oracle.jdbc.OracleDriver";
    public static final String SQL = "select 1 from dual";

    private final IMdEncryptText encryptText = new MdEncryptText();

    protected Map<String, String> getCredential() {
        Map<String, String> credential = new HashMap<>();
        credential.put("username", getUsername());
        credential.put("password", getPassword());
        return credential;
    }

    protected String decrypt(String encrypt) {
        return encryptText.dataS(encrypt);
    }

    protected void refreshIdentify() {
        Map<String, String> credential = getCredential();
        credential.put("password", decrypt(credential.get("password")));
        setDriverClassName(DRIVER);
        setUsername(credential.get("username").toUpperCase());
        setPassword(credential.get("password"));
    }

    @Override
    protected void autoconfigure() throws SQLException {
        refreshIdentify();
        setValidationQuery(SQL);
        super.autoconfigure();
    }

    public static void main(String[] args) {
        IMdEncryptText met = new MdEncryptText();
        System.out.println(met.dataS("2A62BED080E5C5B6====CF33987C38995C18++++"));
        System.out.println(met.EnSysPassWord("9999","sd_hospital"));
        System.out.println(met.EnPassWord(4207, "02167"));//cxzx-admin2021
    }

}
