import Header from '../components/Header'
import LoginForm from '../components/LoginForm'
import Footer from '../components/Footer'

function Login() {
  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1">
        <LoginForm />
      </main>
      <Footer />
    </div>
  )
}

export default Login