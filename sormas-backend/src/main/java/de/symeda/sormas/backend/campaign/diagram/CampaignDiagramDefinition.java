package de.symeda.sormas.backend.campaign.diagram;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramSeries;
import de.symeda.sormas.api.campaign.diagram.DiagramType;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Entity
@Audited
public class CampaignDiagramDefinition extends AbstractDomainObject {

	private static final long serialVersionUID = 7360131476160449930L;

	public static String DIAGRAM_ID = "diagramId";

	private String diagramId;
	private String diagramCaption;
	private DiagramType diagramType;
	private List<CampaignDiagramSeries> campaignDiagramSeries;
	private List<CampaignDiagramSeries> campaignSeriesTotal;
	private boolean percentageDefault;

	@Column
	public String getDiagramId() {
		return diagramId;
	}

	public void setDiagramId(String diagramId) {
		this.diagramId = diagramId;
	}

	@Column
	public String getDiagramCaption() {
		return diagramCaption;
	}

	public void setDiagramCaption(String diagramCaption) {
		this.diagramCaption = diagramCaption;
	}

	@Enumerated(EnumType.STRING)
	public DiagramType getDiagramType() {
		return diagramType;
	}

	public void setDiagramType(DiagramType diagramType) {
		this.diagramType = diagramType;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public List<CampaignDiagramSeries> getCampaignDiagramSeries() {
		return campaignDiagramSeries;
	}

	public void setCampaignDiagramSeries(List<CampaignDiagramSeries> campaignDiagramSeries) {
		this.campaignDiagramSeries = campaignDiagramSeries;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public List<CampaignDiagramSeries> getCampaignSeriesTotal() {
		return campaignSeriesTotal;
	}

	public void setCampaignSeriesTotal(List<CampaignDiagramSeries> campaignSeriesTotal) {
		this.campaignSeriesTotal = campaignSeriesTotal;
	}

	@Column
	public boolean isPercentageDefault() {
		return percentageDefault;
	}

	public void setPercentageDefault(boolean percentageDefault) {
		this.percentageDefault = percentageDefault;
	}
}
