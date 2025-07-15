package com.ivyteam.incremental.build;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name = "build-data-class", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BuildDataClassMojo extends AbstractMojo {

  private BuildContext buildContext;

  @Inject
  public BuildDataClassMojo(BuildContext buildContext) {
    this.buildContext = buildContext;
  }

  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var scanner = buildContext.newScanner(project.getBasedir());
    scanner.setIncludes(new String[] { "**/*.ivyClass" });
    scanner.scan();
    var includedFiles = scanner.getIncludedFiles();
    if (includedFiles == null) {
      return;
    }
    var sourceDirectory = createAndGetSourceDirectory();
    for (var includedFile : includedFiles) {
      var dataClassSubPath = Path.of(includedFile);
      var dataClassFile = scanner.getBasedir().toPath().resolve(dataClassSubPath).toFile();
      if (buildContext.hasDelta(dataClassFile)) {
        var dataClassName = FileNameUtils.getBaseName(dataClassSubPath);
        var javaFile = getJavaFile(sourceDirectory, dataClassSubPath, dataClassName);
        try (var os = buildContext.newFileOutputStream(javaFile.toFile())) {
          var content = """
              public class %s {
                String now = "%s";
              }
                """.formatted(dataClassName, new Date().toString());
          os.write(content.getBytes(Charset.forName("UTF-8")));
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
  }

  private Path getJavaFile(File sourceDirectory, Path dataClassSubPath, String dataClassName) {
    var javaFile = sourceDirectory.toPath();
    if (dataClassSubPath.getNameCount() > 2) {
      javaFile = javaFile.resolve(dataClassSubPath.subpath(1, dataClassSubPath.getNameCount() - 1));
    }
    javaFile = javaFile.resolve(dataClassName + ".java");
    try {
      Files.createDirectories(javaFile.getParent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return javaFile;
  }

  private File createAndGetSourceDirectory() {
    try {
      var sourceDirectory = new File(project.getBasedir(), "src_dataClasses");
      Files.createDirectories(sourceDirectory.toPath());
      return sourceDirectory;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}