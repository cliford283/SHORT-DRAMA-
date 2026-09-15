export const SAMPLE_VIDEOS = [
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
  "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
];

export function generateInitialEpisodes(dramaId, count) {
  const episodes = [];
  for (let ep = 1; ep <= count; ep++) {
    const videoUrl = SAMPLE_VIDEOS[(ep - 1) % SAMPLE_VIDEOS.length];
    episodes.push({
      id: `${dramaId}_ep_${ep}`,
      dramaId: dramaId,
      episodeNumber: ep,
      title: `Episode ${ep}`,
      durationSeconds: 90 + ((ep * 7) % 50),
      videoUrl: videoUrl,
      isUploaded: false
    });
  }
  return episodes;
}

export const INITIAL_DRAMAS = [
  {
    id: "drama_mafia_don",
    title: "The Mafia Don Hired Me as His Wife",
    subtitle: "COVER STORY",
    genre: "Mafia Romance",
    coverUrl: "/posters/poster_mafia_don_1789461041545.jpg",
    description: "Contracted into the lion's den! Down-on-her-luck baker Clara signs a 1-year marriage deed to protect her sick brother, only to discover her billionaire client is the deadliest syndicate boss in New York. Underneath his cold exterior lies an obsessive heart that will burn down empires to keep her safe.",
    tags: ["CONTRACT MARRIAGE", "MAFIA BOSS", "BILLIONAIRE", "OBSESSIVE LOVE"],
    episodesCount: 139,
    isTrending: true,
    isFeatured: true,
    isNewRelease: false,
    rating: 4.9,
    views: "2.4M",
    episodes: generateInitialEpisodes("drama_mafia_don", 139)
  },
  {
    id: "drama_bugatti",
    title: "Get Out of My Bugatti",
    subtitle: "BILINGUAL EDITION",
    genre: "Billionaire Revenge",
    coverUrl: "/posters/poster_bugatti_1789461062516.jpg",
    description: "Disguised as a broke valet driver, hidden heir Logan Vance tests his arrogant fiancee's loyalty on her birthday. When she dumps him on the roadside and climbs into another man's car, Logan calls his butler and deploys 20 gold Bugattis to intercept them.",
    tags: ["HIDDEN IDENTITY", "SWEET REVENGE", "BILLIONAIRE HEIR", "SLAP IN THE FACE"],
    episodesCount: 40,
    isTrending: true,
    isFeatured: true,
    isNewRelease: false,
    rating: 4.8,
    views: "1.9M",
    episodes: generateInitialEpisodes("drama_bugatti", 40)
  },
  {
    id: "drama_poisoned_love",
    title: "Poisoned by His Love",
    subtitle: "EXCLUSIVE DRAMA",
    genre: "Flash Marriage",
    coverUrl: "/posters/poster_poisoned_love_1789461081803.jpg",
    description: "On her half-sister Victoria's wedding day, 25-year-old nurse Lily Watson is forced to marry Dominic Castellano, a ruthless mafia king rumored to have murdered his last wife. When Victoria flees, Dominic's hitmen threaten Lily's family. To save them, Lily takes Victoria's place at the altar—only to become the bride of a man she fears may be a killer.",
    tags: ["HIDDEN IDENTITY", "FLASH MARRIAGE", "LOVE AFTER MARRIAGE", "REVENGE"],
    episodesCount: 56,
    isTrending: true,
    isFeatured: true,
    isNewRelease: true,
    rating: 4.9,
    views: "3.1M",
    episodes: generateInitialEpisodes("drama_poisoned_love", 56)
  },
  {
    id: "drama_alpha_queen",
    title: "Oops, You Bullied The Alpha Queen",
    subtitle: "HOT WEREWOLF",
    genre: "Fantasy Werewolf",
    coverUrl: "/posters/poster_alpha_queen_1789461100658.jpg",
    description: "Transferred to St. Jude's Academy as a seemingly quiet scholarship student, Elena endured months of torment from the elite cheer captain. But the blood moon triggers her awakening as the supreme Royal Blood Lycan Queen whose roar forces packs to kneel.",
    tags: ["ALPHA QUEEN", "WEREWOLF", "CAMPUS REVENGE", "SHIFTER ROMANCE"],
    episodesCount: 45,
    isTrending: true,
    isFeatured: true,
    isNewRelease: false,
    rating: 4.7,
    views: "1.5M",
    episodes: generateInitialEpisodes("drama_alpha_queen", 45)
  },
  {
    id: "drama_cowgirl",
    title: "This Cowgirl Defended My Autistic Stepson",
    subtitle: "HEARTWARMING ROMANCE",
    genre: "Country Billionaire",
    coverUrl: "/posters/poster_cowgirl_1789461149055.jpg",
    description: "Hired as a ranch hand, sharpshooter cowgirl Sadie shields tech tycoon Liam's non-verbal son from greedy relatives. When the young boy speaks his very first sentence to Sadie, the cold billionaire realizes this fiery country girl is the missing piece of his shattered home.",
    tags: ["RANCH ROMANCE", "PROTECTOR", "BILLIONAIRE DAD", "FAMILY BOND"],
    episodesCount: 32,
    isTrending: true,
    isFeatured: false,
    isNewRelease: false,
    rating: 4.8,
    views: "980K",
    episodes: generateInitialEpisodes("drama_cowgirl", 32)
  },
  {
    id: "drama_archmage",
    title: "Bow To 10-Year-Old Archmage Aldric",
    subtitle: "MAGIC REBIRTH",
    genre: "Fantasy Magic",
    coverUrl: "/posters/poster_archmage_1789461165800.jpg",
    description: "Betrayed by the Imperial Council after saving humanity from the void dragon, Archmage Aldric awakes reincarnated inside the body of a 10-year-old exiled noble orphan. With 500 years of forbidden spell knowledge intact, he dismantles aristocratic bullies before lunch.",
    tags: ["REINCARNATION", "MAGIC PRODIGY", "OVERPOWERED HERO", "FANTASY EMPIRE"],
    episodesCount: 130,
    isTrending: true,
    isFeatured: false,
    isNewRelease: true,
    rating: 4.9,
    views: "2.8M",
    episodes: generateInitialEpisodes("drama_archmage", 130)
  },
  {
    id: "drama_lycan_queen",
    title: "The Rise of the Lycan Queen",
    subtitle: "SUPREME MONARCH",
    genre: "Fantasy Werewolf",
    coverUrl: "/posters/poster_lycan_queen_1789461119081.jpg",
    description: "Banished into the cursed silver woods by her wicked pack elders, scarred hybrid Astrid unlocks the legendary primal fire of the First Moon. As three rival alpha kings march to conquer her territory, she crowns herself Empress.",
    tags: ["LYCAN QUEEN", "TRUE ALPHA", "ENEMIES TO LOVERS", "FANTASY WAR"],
    episodesCount: 51,
    isTrending: false,
    isFeatured: false,
    isNewRelease: true,
    rating: 4.6,
    views: "1.2M",
    episodes: generateInitialEpisodes("drama_lycan_queen", 51)
  },
  {
    id: "drama_weakest_bastard",
    title: "The Weakest Bastard Shakes the World",
    subtitle: "ACTION EPICS",
    genre: "Martial Arts Fantasy",
    coverUrl: "/posters/poster_weakest_bastard_1789461185110.jpg",
    description: "Deemed born with defective mana veins and discarded into the salt mines, illegitimate son Arthur stumbles upon the sealed tomb of the Immortal God of War. Armed with forbidden blade arts, his return shocks the entire continent.",
    tags: ["MARTIAL ARTS", "ZERO TO HERO", "DARK FANTASY", "IMMORTAL SWORD"],
    episodesCount: 68,
    isTrending: false,
    isFeatured: false,
    isNewRelease: false,
    rating: 4.7,
    views: "890K",
    episodes: generateInitialEpisodes("drama_weakest_bastard", 68)
  }
];
