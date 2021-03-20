package de.symeda.sormas.backend.campaign.statistics;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsFacade;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.campaign.data.CampaignFormData;
import de.symeda.sormas.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "CampaignStatisticsFacade")
public class CampaignStatisticsFacadeEjb implements CampaignStatisticsFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@Override
	public List<CampaignStatisticsDto> queryCampaignStatistics(
		CampaignFormDataCriteria criteria,
		Integer first,
		Integer max,
		List<SortProperty> sortProperties) {

		Query campaignsStatisticsQuery = em.createNativeQuery(buildStatisticsQuery());
		List<CampaignStatisticsDto> results = ((Stream<Object[]>) campaignsStatisticsQuery.getResultStream()).map(
			result -> new CampaignStatisticsDto(
				(String) result[0],
				(String) result[1],
				(String) result[2],
				(String) result[3],
				(String) result[4],
				0))
			.collect(Collectors.toList());
		return results;

	}

	@Override
	public long count(CampaignFormDataCriteria criteria) {
		return 10;
	}

	private String buildStatisticsQuery() {
		StringBuilder selectBuilder = new StringBuilder("SELECT ").append(buildSelectField(Campaign.TABLE_NAME, Campaign.NAME))
			.append(", ")
			.append(buildSelectField(CampaignFormMeta.TABLE_NAME, CampaignFormMeta.FORM_NAME))
			.append(", ")
			.append(buildSelectField(Region.TABLE_NAME, Region.NAME))
			.append(", ")
			.append(buildSelectField(District.TABLE_NAME, District.NAME))
			.append(", ")
			.append(buildSelectField(Community.TABLE_NAME, Community.NAME))
			.append(" FROM ")
			.append(CampaignFormData.TABLE_NAME);

		StringBuilder joinBuilder = new StringBuilder();
		joinBuilder.append(" LEFT JOIN ")
			.append(Campaign.TABLE_NAME)
			.append(" ON ")
			.append(CampaignFormData.TABLE_NAME)
			.append(".")
			.append(CampaignFormData.CAMPAIGN)
			.append("_id = ")
			.append(Campaign.TABLE_NAME)
			.append(".")
			.append(Campaign.ID);

		joinBuilder.append(" LEFT JOIN ")
			.append(CampaignFormMeta.TABLE_NAME)
			.append(" ON ")
			.append(CampaignFormData.TABLE_NAME)
			.append(".")
			.append(CampaignFormData.CAMPAIGN_FORM_META)
			.append("_id = ")
			.append(CampaignFormMeta.TABLE_NAME)
			.append(".")
			.append(CampaignFormMeta.ID);

		joinBuilder.append(" LEFT JOIN ")
			.append(Region.TABLE_NAME)
			.append(" ON ")
			.append(CampaignFormData.TABLE_NAME)
			.append(".")
			.append(CampaignFormData.REGION)
			.append("_id = ")
			.append(Region.TABLE_NAME)
			.append(".")
			.append(Region.ID);

		joinBuilder.append(" LEFT JOIN ")
			.append(District.TABLE_NAME)
			.append(" ON ")
			.append(CampaignFormData.TABLE_NAME)
			.append(".")
			.append(CampaignFormData.DISTRICT)
			.append("_id = ")
			.append(District.TABLE_NAME)
			.append(".")
			.append(District.ID);

		joinBuilder.append(" LEFT JOIN ")
			.append(Community.TABLE_NAME)
			.append(" ON ")
			.append(CampaignFormData.TABLE_NAME)
			.append(".")
			.append(CampaignFormData.COMMUNITY)
			.append("_id = ")
			.append(Community.TABLE_NAME)
			.append(".")
			.append(Community.ID);

		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(selectBuilder).append(joinBuilder);

		return queryBuilder.toString();
	}

	private String buildSelectField(String tableName, String fieldName) {
		StringBuilder selectFieldBuilder = new StringBuilder();
		selectFieldBuilder.append(tableName).append(".").append(fieldName).append(" AS ").append(tableName).append("_").append(fieldName);
		return selectFieldBuilder.toString();
	}

	@LocalBean
	@Stateless
	public static class CampaignStatisticsFacadeEjbLocal extends CampaignStatisticsFacadeEjb {
	}
}
