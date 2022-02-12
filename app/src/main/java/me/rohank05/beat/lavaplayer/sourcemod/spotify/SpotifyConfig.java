/**
 * The code below is taken from Topi-Source-Manager-Plugin.
 * https://github.com/Topis-Lavalink-Plugins/Topis-Source-Managers-Plugin
 * All credit to this piece of code goes to TopiSenpai
 * https://github.com/TopiSenpai
 */

package me.rohank05.beat.lavaplayer.sourcemod.spotify;

import com.neovisionaries.i18n.CountryCode;

public class SpotifyConfig {
    private String clientId;
    private String clientSecret;
    private CountryCode countryCode = CountryCode.US;

    public SpotifyConfig() {
    }

    public SpotifyConfig(String clientId, String clientSecret, CountryCode countryCode) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.countryCode = countryCode;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public CountryCode getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = CountryCode.getByCode(countryCode);
    }
}
