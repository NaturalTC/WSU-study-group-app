import Header from '../components/Header'
import RegisterForm from '../components/RegisterForm'
import Footer from '../components/Footer'

function Register() {
  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-1">
        <RegisterForm />
      </main>
      <Footer />
    </div>
  )
}

export default Register