package com.wow.timewalkers.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "timewalking_events")
public class TimewalkingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expansion;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_turbulent_timeways", nullable = false)
    private boolean turbulentTimeways;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExpansion() { return expansion; }
    public void setExpansion(String expansion) { this.expansion = expansion; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isTurbulentTimeways() { return turbulentTimeways; }
    public void setTurbulentTimeways(boolean turbulentTimeways) { this.turbulentTimeways = turbulentTimeways; }
}
