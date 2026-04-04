<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <section class="fade-in">
    <div class="carousel-container">
      <div class="carousel-wrapper" :style="{ transform: `translateX(-${dash.currentSlide * 100}%)` }">
        <div v-for="(slide, index) in dash.carouselSlides" :key="index" class="carousel-slide">
          <img :src="slide.image" :alt="slide.title" class="carousel-image" />
          <div class="carousel-content">
            <h2>{{ slide.title }}</h2>
            <p>{{ slide.description }}</p>
            <button class="carousel-btn" @click="dash.goToLink(slide.link)">了解更多</button>
          </div>
        </div>
      </div>

      <div class="carousel-dots">
        <span
          v-for="(slide, index) in dash.carouselSlides"
          :key="index"
          class="dot"
          :class="{ active: dash.currentSlide === index }"
          @click="dash.currentSlide = index"
        ></span>
      </div>

      <button class="carousel-arrow prev" @click="dash.prevSlide">❮</button>
      <button class="carousel-arrow next" @click="dash.nextSlide">❯</button>
    </div>

    <div class="home-sections">
      <div class="section-block" v-for="(section, index) in dash.homeData" :key="index">
        <div class="section-title-bar">
          <h3>{{ section.title }}</h3>
          <span class="more-link" @click="dash.activeMenu = section.target">查看更多 ></span>
        </div>
        <div class="card-grid">
          <div class="card" v-for="(item, i) in section.items" :key="i" @click="dash.handleCardClick(item)">
            <div
              class="card-cover"
              :style="
                item.coverImg
                  ? {
                      backgroundImage: `url(${item.coverImg})`,
                      backgroundSize: 'cover',
                      backgroundPosition: 'center',
                    }
                  : { background: item.color || 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)' }
              "
            >
              <span class="type-badge" :class="item.type">{{ item.typeText }}</span>
              <div v-if="item.type === 'video'" class="play-overlay">▶</div>
            </div>
            <div class="card-info">
              <h4 class="card-title">{{ item.title }}</h4>
              <p class="card-desc">{{ item.author || item.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
