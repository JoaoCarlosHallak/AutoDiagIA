package com.autodiag.AutoDiagIA.entity;
import jakarta.persistence.*;


@Entity
@Table(name = "session_pids")
public class SessionPid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String pid;

    public SessionPid() {
    }

    public SessionPid(Session session, String pid) {
        this.session = session;
        this.pid = pid;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}