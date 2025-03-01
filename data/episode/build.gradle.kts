plugins {
	alias(libs.plugins.mapisode.data)
}

android {
	namespace = "com.boostcamp.mapisode.episode"
}

dependencies {
	implementation(projects.core.common)
	implementation(projects.domain.episode)
	implementation(projects.core.firebase)
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.firestore)
	implementation(libs.firebase.storage)
}
