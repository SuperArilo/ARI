package com.tty;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("ALL")
public class Loader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder pluginClasspathBuilder) {

        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://maven-central.storage-download.googleapis.com/maven2").build());

        pluginClasspathBuilder.addLibrary(resolver);
    }

}
