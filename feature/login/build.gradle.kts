plugins {
	alias(libs.plugins.mapisode.feature)
}

android {
	namespace = "com.boostcamp.mapisode.login"
}

dependencies {
	implementation(projects.core.auth)
	implementation(projects.domain.user)
	implementation(projects.domain.mygroup)
	implementation(libs.bundles.coil)
}
