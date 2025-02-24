plugins {
	alias(libs.plugins.mapisode.feature)
}

android {
	namespace = "com.boostcamp.mapisode.episode"
}

dependencies {
	implementation(project.libs.bundles.naverMap)
	implementation(project.libs.bundles.coil)
	implementation(projects.core.network)
	implementation(projects.domain.episode)
	implementation(projects.domain.mygroup)
}
