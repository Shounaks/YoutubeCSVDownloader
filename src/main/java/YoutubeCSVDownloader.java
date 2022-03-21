import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import entity.SongMetadata;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YoutubeCSVDownloader {

    private static final Path DOWNLOAD_PATH = Paths.get("youtube_songs");
    private static final String CSV_LOCATION = "song_playlist.csv";
    private static final CsvMapper CSV_MAPPER = new CsvMapper();
    private static final CsvSchema CSV_SCHEMA = CSV_MAPPER.schemaFor(SongMetadata.class);
    private static final Logger LOGGER = Logger.getLogger("logfile");

    public static void main(String[] args) {
        List<SongMetadata> songs = getSongInfoList().subList(1, 2);
        LOGGER.log(Level.INFO, "Parsed CSV to Object!!");
        songs.forEach(YoutubeCSVDownloader::downloadSongs);
    }


    private static List<SongMetadata> getSongInfoList() {
        try (FileReader fr = new FileReader(ClassLoader.getSystemResource(CSV_LOCATION).getFile())) {
            LOGGER.log(Level.INFO, "CSV File found !!");
            return CSV_MAPPER
                    .readerFor(SongMetadata.class)
                    .with(CSV_SCHEMA)
                    .<SongMetadata>readValues(fr)
                    .readAll()
                    //skip first line since that is schema for csv file
                    .stream().skip(1).toList();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "CSV file not found...");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Parsing Failed, check input file again");
        }
        return List.of();
    }

    private static void downloadSongs(SongMetadata song) {
        try {
            Files.createDirectories(DOWNLOAD_PATH);//executes 1 time
            String log = String.format("Downloading %s from Anime: %s", song.getSongName(), song.getAnimeName());
            LOGGER.log(Level.INFO, log);
            YoutubeDLRequest request = generateYoutubeDLRequest(song);
            YoutubeDL.execute(request);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private static YoutubeDLRequest generateYoutubeDLRequest(SongMetadata song) {
        YoutubeDLRequest request = new YoutubeDLRequest(song.getYoutubeLink(), YoutubeCSVDownloader.DOWNLOAD_PATH.toString());
        request.setOption("ignore-errors");        // --ignore-errors
        request.setOption("output", song.getSongName().trim().replace(" ", "_") + ".mp4");    // --output "%(id)s"
        request.setOption("retries", 10);        // --retries 10
        return request;
    }
}