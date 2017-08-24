package ru.zrv.tazacom.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.haulmont.chile.core.annotations.NamePattern;
import javax.validation.constraints.NotNull;

@NamePattern("%s %s|url,date")
@Table(name = "TAZACOM_IMPORTING")
@Entity(name = "tazacom$Importing")
public class Importing extends StandardEntity {
    private static final long serialVersionUID = 1157822451151516649L;

    @NotNull
    @Column(name = "STATE", nullable = false)
    protected String state;

    @Column(name = "URL")
    protected String url;

    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_", nullable = false)
    protected Date date;

    @Column(name = "QUANTITY")
    protected Integer quantity;

    public StateEnum getState() {
        return state == null ? null : StateEnum.fromId(state);
    }

    public void setState(StateEnum state) {
        this.state = state == null ? null : state.getId();
    }



    public void setDate(Date date) {
        this.date = date;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Date getDate() {
        return date;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }


}