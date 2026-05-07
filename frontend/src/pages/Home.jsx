import Header from '../components/Header'
import AppHeader from '../components/AppHeader'
import HeroSection from '../components/HeroSection'
import FeaturesSection from '../components/FeaturesSection'
import Footer from '../components/Footer'
import { useAuth } from '../context/AuthContext'

function Home() {
  const { profile, loading } = useAuth()

  return (
    <div className="flex flex-col min-h-screen bg-white dark:bg-gray-900 transition-colors duration-300">
      {!loading && (profile ? <AppHeader /> : <Header />)}
      <main className="flex-1">
        <HeroSection />
        <FeaturesSection />
      </main>
      <Footer />
    </div>
  )
}

export default Home
