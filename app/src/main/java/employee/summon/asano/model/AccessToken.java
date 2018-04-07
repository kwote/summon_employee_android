package employee.summon.asano.model;

import java.util.Date;

public class AccessToken {
    private String id;
    private Integer ttl;
    private Date created;
    private Integer personId;

    public AccessToken(String id, Integer ttl, Date created, Integer personId) {
        this.id = id;
        this.ttl = ttl;
        this.created = created;
        this.personId = personId;
    }

    public String getId() {
        return id;
    }

    public Integer getTtl() {
        return ttl;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getPersonId() {
        return personId;
    }
}
