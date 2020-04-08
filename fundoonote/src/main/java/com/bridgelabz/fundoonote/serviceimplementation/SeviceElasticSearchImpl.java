package com.bridgelabz.fundoonote.serviceimplementation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bridgelabz.fundoonote.entity.Noteinfo;
import com.bridgelabz.fundoonote.service.IServiceElasticSearch;
import com.bridgelabz.fundoonote.utility.JwtGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SeviceElasticSearchImpl implements IServiceElasticSearch {
	String INDEX = "fundoo";
	String TYPE = "notes";
	@Autowired
	private RestHighLevelClient client;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtGenerator tokenservice;

	@Override
	public String createNote(Noteinfo note) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String, Object> documentMappper = objectMapper.convertValue(note, Map.class);

//		CreateIndexRequest request = new CreateIndexRequest(INDEX);
//	    request.mapping("_doc", XContentType.JSON);
//	    CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
	    
		IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, String.valueOf(note.getNid())).source(documentMappper);
		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		return indexResponse.getResult().name();
	}

	@Override
	public Noteinfo findById(String id) throws Exception {

		GetRequest getRequest = new GetRequest(INDEX, TYPE, id);

		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		Map<String, Object> resultMap = getResponse.getSource();

		return objectMapper.convertValue(resultMap, Noteinfo.class);

	}

	@Override
	public String upDateNote(Noteinfo note) throws Exception {
		Noteinfo notes = findById(String.valueOf(note.getNid()));

		UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, String.valueOf(notes.getNid()));

		@SuppressWarnings("unchecked")
		Map<String, Object> mapDoc = objectMapper.convertValue(note, Map.class);
		updateRequest.doc(mapDoc);

		UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
		return updateResponse.getResult().name();
	}

	@Override
	public String deleteNote(String id) throws IOException {

		DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, id);
		DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);

		return response.getResult().name();
	}

	private List<Noteinfo> getSearchResult(SearchResponse searchResponse) {
		System.out.println("*********Response************");
		SearchHit[] searchHits = searchResponse.getHits().getHits();
		List<Noteinfo> notesDoc = new ArrayList<Noteinfo>();
		if (searchHits.length > 0) {
			Arrays.stream(searchHits)
					.forEach(hit -> notesDoc.add(objectMapper.convertValue(hit.getSourceAsMap(), Noteinfo.class)));
		}

		return notesDoc;

	}

	@Override
	public List<Noteinfo> searchByTitle(String title, String token) throws IOException {
		System.out.println("************");
		Long userId = tokenservice.parseJWT(token);
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("*", title));
				//.filter(QueryBuilders.termsQuery("userId", String.valueOf(userId)));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = null;

		response = client.search(searchRequest, RequestOptions.DEFAULT);

		return getSearchResult(response);
	}

}