package com.tty;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class Loader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder pluginClasspathBuilder) {

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer", "HikariCP", "jar", "5.1.0"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.sql2o", "sql2o", "jar", "1.9.1"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.mysql", "mysql-connector-j", "jar", "9.5.0"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.baomidou", "mybatis-plus-core", "jar", "3.5.15"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.baomidou", "mybatis-plus-extension", "jar", "3.5.15"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.baomidou", "mybatis-plus-jsqlparser", "jar", "3.5.15"), "provided"));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.javalin", "javalin", "jar", "6.7.0"), "provided"));

        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        pluginClasspathBuilder.addLibrary(resolver);
    }

}
