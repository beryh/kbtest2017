package com.yh9589.service;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yh9589.data.MovieRepository;
import com.yh9589.domain.Movie;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.NumberUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by beryh on 2017-02-15.
 */
@Service
public class DaumMovieServiceImpl implements MovieService {
    @Resource
    private MovieRepository movieRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private Environment env;

    @Override
    public SearchResult getMovieListByQuery(String query, Integer page, Integer count) {
        if ("".equals(query) || page <= 0 || count <= 0)
            return new SearchResult();

        URI uri = UriComponentsBuilder.newInstance().scheme("https").host("apis.daum.net").path("/contents/movie")
                .queryParam("apiKey", env.getProperty("daum_api_key"))
                .queryParam("q", query)
                .queryParam("output", "json")
                .queryParam("pageno", page)
                .queryParam("result", count)
                .build()
                .encode()
                .toUri();

        String jsonResponse = restTemplate.getForObject(uri, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addDeserializer(SearchResult.class, new DaumMovieResponseDeserializer()));

        try {
            return objectMapper.readValue(jsonResponse, SearchResult.class);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public void save(Movie movie) {
        movieRepository.saveAndFlush(movie);
    }

    @Override
    public Optional<Movie> findOne(Long id) {
        return Optional.ofNullable(movieRepository.findOne(id));
    }

    static class DaumMovieResponseDeserializer extends StdDeserializer<SearchResult> {
        public DaumMovieResponseDeserializer() {
            this(null);
        }

        protected DaumMovieResponseDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public SearchResult deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode result = (JsonNode)p.getCodec().readTree(p).get("channel");
            // total count
            Integer count = result.path("result").asInt();
            Integer totalCount = result.path("totalCount").asInt();
            Integer page = result.path("page").asInt();

            ArrayNode node = (ArrayNode) result.path("item");
            ObjectMapper objectMapper = new ObjectMapper();

            List<Movie> movieList = new ArrayList<>();
            for(JsonNode j: node) {
                objectMapper.treeToValue(j, DaumMovie.class).getMovie().ifPresent(m -> movieList.add(m));
            }

            return new SearchResult(movieList, totalCount, count, page);
        }
    }

    static class DaumMovie {
        private Movie movie;
        private Map<String, Object> objMap;

        public DaumMovie() {
            movie = new Movie();
            objMap = new HashMap<>();
        }

        @JsonAnySetter
        public void anySetter(String name, Object value) {
            name = adjustFieldName(name);
            try {
                if (List.class.isAssignableFrom(movie.getClass().getDeclaredField(name).getType())) {
                    List<String> listValue;
                    // save to list map
                    if (value instanceof ArrayList) {
                        listValue = extractContentToStringList((List<LinkedHashMap>) value);

                        if (listValue.isEmpty()) return;
                    } else {
                        if (Optional.ofNullable(objMap.get(name)).isPresent() && objMap.get(name) instanceof List) {
                            listValue = (List<String>) objMap.get(name);
                        } else {
                            listValue = new ArrayList<>();
                        }

                        String content = extractContentToString((LinkedHashMap) value);
                        if ("".equals(content)) return;

                        listValue.add(content);
                    }

                    objMap.put(name, listValue);
                } else {
                    // save to single map
                    String stringValue;
                    if (value instanceof ArrayList) {
                        stringValue = extractContentToString((List<LinkedHashMap>) value, 0);
                    } else {
                        stringValue = extractContentToString((LinkedHashMap) value);
                    }

                    if ("".equals(stringValue)) return;

                    objMap.put(name, stringValue);
                }
            } catch (NoSuchFieldException e) {
                // SKIP UNDECLARED FIELDS
                return;
            }
        }

        private String adjustFieldName(String name) {
            switch (name) {
                case "photo1":
                case "photo2":
                case "photo3":
                case "photo4":
                case "photo5":
                    return "photo";
            }

            return name;
        }

        private List<String> extractContentToStringList(List<LinkedHashMap> j) {
            return j.stream().map(v -> v.get("content").toString()).filter(v -> !("".equals(v))).collect(Collectors.toList());
        }

        private String extractContentToString(List<LinkedHashMap> j, Integer index) {
            return j.get(index).get("content").toString();
        }

        private String extractLinkToString(List<LinkedHashMap> j, Integer index) {
            return j.get(index).get("link").toString();
        }

        private String extractContentToString(LinkedHashMap j) {
            return j.get("content").toString();
        }

        @JsonProperty("title")
        public void setTitle(List<LinkedHashMap> j) {
            objMap.put("title", extractContentToString(j, 0));
            objMap.put("link", extractLinkToString(j, 0));

            MultiValueMap<String, String> parameters;
            try {
                parameters = UriComponentsBuilder.fromUri(new URI(objMap.get("link").toString())).build().getQueryParams();
                objMap.put("id", parameters.get("movieId").get(0));
            } catch (URISyntaxException e) {
                objMap.put("id", null);
            }
        }

        @JsonProperty("grades")
        public void setRating(List<LinkedHashMap> j) {
            Double rating = 0.0D;
            try {
                rating = NumberUtils.parseNumber(extractContentToString(j, 0), Double.class);
            } catch(NumberFormatException e) {
                rating = 0.0D;
            } finally {
                objMap.put("rating", rating);
            }
        }

        public Optional<Movie> getMovie() {
            try {
                BeanUtils.populate(movie, objMap);
            } catch (Exception e) {
                e.printStackTrace();
                movie.setId(null);
            }

            if(movie.getId() == null) return Optional.empty();
            return Optional.ofNullable(movie);
        }
    }
}
