exports.run = async (interaction) => {
    const client = interaction.client;
    const query = interaction.options.getString("song");
    const player = client.moon.players.create({
        guildId: interaction.guild.id,
        voiceChannel: interaction.member.voice.channel.id,
        textChannel: interaction.channel.id,
        autoPlay: true,
    });
    if (!player.connected) {
        // Connecting to the voice channel if not already connected
        player.connect({
            setDeaf: true,
            setMute: false,
        });
    }
    let res = await client.moon.search({
        source: "youtubemusic",
        query: query,
    });
    if (res.loadType === "loadfailed") {
        // Responding with an error message if loading fails
        return interaction.reply({
            content: `:x: Load failed - the system is not cooperating.`,
        });
    } else if (res.loadType === "empty") {
        // Responding with a message if the search returns no results
        return interaction.reply({
            content: `:x: No matches found!`,
        });
    }

    if (res.loadType === "playlist") {
        interaction.reply({
            content: `${res.playlistInfo.name} This playlist has been added to the waiting list, spreading joy`,
        });

        for (const track of res.tracks) {
            // Adding tracks to the queue if it's a playlist
            player.queue.add(track);
        }
    } else {
        player.queue.add(res.tracks[0]);
        interaction.reply({
            content: `${res.tracks[0].title} was added to the waiting list`,
        });
    }

    if (!player.playing) {
        // Starting playback if not already playing
        player.play();
    }
};
