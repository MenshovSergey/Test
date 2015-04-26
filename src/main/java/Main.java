
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        String targetClass = args[1];
        String targetDir = args[0];
        String outDir = args[2];
        File f = new File(targetDir);
        Solution solve = new Solution(f);
        File out = new File(outDir);
        Map<File, String> ans = solve.getResult(targetClass);
        //System.out.println(ans.toString());

        for (File file : ans.keySet()) {
            String packageFile = ans.get(file);
            new File(out.getAbsolutePath() +"/" + packageFile.replaceAll("\\.","/")).mkdirs();
            File dir = new File(out.getAbsolutePath() +"/" + packageFile.replaceAll("\\.","/") + "/" + file.getName());
            Files.copy(file.toPath(),dir.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }

}
