import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useForm } from 'react-hook-form';

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const { register, handleSubmit, setValue, formState: { errors } } = useForm();
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const fillDemo = (username) => {
        setValue('username', username, { shouldValidate: true });
        setValue('password', 'password123', { shouldValidate: true });
        setError('');
    };

    const onSubmit = async (data) => {
        try {
            setIsLoading(true);
            setError('');
            await login(data.username, data.password);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to login. Please check your credentials.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#FFFBF5] py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-sm border border-[#C3ACD0]/40">
                <div className="text-center">
                    <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-[#F7EFE5] border border-[#C3ACD0]/50 mb-3">
                        <span className="w-4 h-4 rounded-full bg-[#674188]"></span>
                    </div>
                    <h2 className="text-2xl font-bold text-[#281B34] tracking-tight">
                        Sign in to your account
                    </h2>
                    <p className="mt-1.5 text-sm text-[#7A668A]">
                        Smart Complaint Management System
                    </p>
                </div>

                {/* Demo Quick-Fill Bar */}
                <div className="p-3.5 bg-[#F7EFE5] rounded-xl border border-[#C3ACD0]/40">
                    <p className="text-xs font-semibold text-[#674188] uppercase tracking-wider text-center mb-2.5">
                        Demo Accounts (Click to Fill)
                    </p>
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                        <button
                            type="button"
                            onClick={() => fillDemo('reporter')}
                            className="px-2.5 py-1.5 text-xs font-semibold rounded-lg border border-[#C3ACD0] text-[#674188] bg-white hover:bg-[#674188] hover:text-white transition-all text-center shadow-sm"
                        >
                            Reporter
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('coordinator')}
                            className="px-2.5 py-1.5 text-xs font-semibold rounded-lg border border-[#C3ACD0] text-[#674188] bg-white hover:bg-[#674188] hover:text-white transition-all text-center shadow-sm"
                        >
                            Coordinator
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('staff')}
                            className="px-2.5 py-1.5 text-xs font-semibold rounded-lg border border-[#C3ACD0] text-[#674188] bg-white hover:bg-[#674188] hover:text-white transition-all text-center shadow-sm"
                        >
                            Staff
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('admin')}
                            className="px-2.5 py-1.5 text-xs font-semibold rounded-lg border border-[#C3ACD0] text-[#674188] bg-white hover:bg-[#674188] hover:text-white transition-all text-center shadow-sm"
                        >
                            Admin
                        </button>
                    </div>
                </div>

                <form className="mt-6 space-y-6" onSubmit={handleSubmit(onSubmit)}>
                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded-r-lg">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}
                    <div className="rounded-md space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Username or Email</label>
                            <input
                                type="text"
                                {...register('username', { required: 'Username or Email is required' })}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="Enter your username or email"
                            />
                            {errors.username && <span className="text-xs text-red-500 mt-1 block">{errors.username.message}</span>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Password</label>
                            <input
                                type="password"
                                {...register('password', { required: 'Password is required' })}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="Enter your password"
                            />
                            {errors.password && <span className="text-xs text-red-500 mt-1 block">{errors.password.message}</span>}
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="group relative w-full flex justify-center py-2.5 px-4 rounded-lg text-sm font-semibold text-white bg-[#674188] hover:bg-[#543372] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#674188] disabled:opacity-50 transition-colors shadow-sm"
                        >
                            {isLoading ? 'Signing in...' : 'Sign in'}
                        </button>
                    </div>
                    <div className="text-sm text-center">
                        <Link to="/register" className="font-medium text-[#674188] hover:text-[#543372] underline">
                            Don't have an account? Register here
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
