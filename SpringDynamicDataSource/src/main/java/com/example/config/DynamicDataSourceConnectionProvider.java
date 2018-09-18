package com.example.config;

import static java.lang.String.format;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicDataSourceConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3927025737739418106L;
	
	@Autowired
	private final RestTemplate restTemplate;

    public DynamicDataSourceConnectionProvider(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return datasource();
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return datasource();
    }


    private Credentials getCredentials() {
        ResponseEntity<Credentials> response = restTemplate.getForEntity("http://localhost:8080/credentials", Credentials.class);
        return response.getBody();
    }

    private String getUrl(Credentials credentials) {
        String connection = "jdbc:mysql://%s:%s/%s";
        return format(connection, credentials.getHostname(), credentials.getPort(), credentials.getSchema());
    }


    private DataSource datasource() {
        Credentials credentials = getCredentials();

        log.info("New credentials: {}", credentials);

        return DataSourceBuilder.create()
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .url(getUrl(credentials))
                .driverClassName("com.mysql.jdbc.Driver")
                .build();
    }

}
