package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "queries")
public class QueryTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String dateRaised;
    private String raiserName;
    private String raiserType; // Student / Employee
    private String targetType; // e.g., HR, Director, Team Lead
    private String question;
    private String priority;
    @Column(length = 1000)
    private String description;
    
    // Response/Report module details
    private String responder;
    @Column(length = 1000)
    private String responseDescription;
    private String status = "Pending"; // Pending, closed, on hold
    private String holdReason;
    private String holdDate;
    
    // Getters and Setters
    public Long getId()
    { 
    	return id; 
    }
    public void setId(Long id) 
    { 
    	this.id = id; 
    }
    public String getDateRaised() 
    {
    	return dateRaised;
    }
    public void setDateRaised(String dateRaised) 
    { 
    	this.dateRaised = dateRaised;
    }
    public String getRaiserName() 
    { 
    	return raiserName; 
    }
    public void setRaiserName(String raiserName) 
    { 
    	this.raiserName = raiserName; 
    }
    public String getRaiserType() 
    { 
    	return raiserType; 
    }
    public void setRaiserType(String raiserType) 
    { 
    	this.raiserType = raiserType;
    }
    public String getTargetType()
    {
    	return targetType; 
    }
    public void setTargetType(String targetType) 
    { 
    	this.targetType = targetType;
    }
    public String getQuestion() 
    { 
    	return question;
    }
    public void setQuestion(String question) 
    { 
    	this.question = question;
    }
    public String getPriority() 
    { 
    	return priority;
    }
    public void setPriority(String priority)
    { 
    	this.priority = priority; 
    }
    public String getDescription() 
    {
    	return description; 
    }
    public void setDescription(String description) 
    { 
    	this.description = description;
    }
    public String getResponder()
    {
    	return responder; 
    }
    public void setResponder(String responder) 
    { 
    	this.responder = responder; 
    }
    public String getResponseDescription() 
    { 
    	return responseDescription;
    }
    public void setResponseDescription(String responseDescription) 
    { 
    	this.responseDescription = responseDescription;
    }
    public String getStatus()
    {
    	return status; 
    }
    public void setStatus(String status) 
    { 
    	this.status = status;
    }
    public String getHoldReason()
    { 
    	return holdReason;
    }
    public void setHoldReason(String holdReason) 
    {
    	this.holdReason = holdReason;
    }
    public String getHoldDate() 
    {
    	return holdDate;
    }
    public void setHoldDate(String holdDate)
    {
    	this.holdDate = holdDate;
    }
}