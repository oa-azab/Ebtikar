package me.omarahmed.ebtikar.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Dell on 18/05/2018.
 */

public interface EbtikarService {
    @GET("{id}")
    Call<String> getClients(@Path("id") String id);
}
