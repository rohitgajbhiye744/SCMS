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
        <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-md shadow-sm border border-gray-200">
                <div>
                    <h2 className="mt-6 text-center text-2xl font-bold text-gray-900 tracking-tight">
                        Sign in to your account
                    </h2>
                    <p className="mt-2 text-center text-sm text-gray-500">
                        Smart Complaint Management System
                    </p>
                </div>

                {/* Demo Quick-Fill Bar */}
                <div className="p-3 bg-gray-50 rounded-md border border-gray-200">
                    <p className="text-xs font-semibold text-gray-600 uppercase tracking-wider text-center mb-2">
                        Demo Accounts (Click to Fill)
                    </p>
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                        <button
                            type="button"
                            onClick={() => fillDemo('reporter')}
                            className="px-2 py-1.5 text-xs font-medium rounded border border-gray-300 text-gray-700 bg-white hover:bg-gray-100 hover:border-gray-400 transition-colors text-center shadow-sm"
                        >
                            Reporter
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('coordinator')}
                            className="px-2 py-1.5 text-xs font-medium rounded border border-gray-300 text-gray-700 bg-white hover:bg-gray-100 hover:border-gray-400 transition-colors text-center shadow-sm"
                        >
                            Coordinator
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('staff')}
                            className="px-2 py-1.5 text-xs font-medium rounded border border-gray-300 text-gray-700 bg-white hover:bg-gray-100 hover:border-gray-400 transition-colors text-center shadow-sm"
                        >
                            Staff
                        </button>
                        <button
                            type="button"
                            onClick={() => fillDemo('admin')}
                            className="px-2 py-1.5 text-xs font-medium rounded border border-gray-300 text-gray-700 bg-white hover:bg-gray-100 hover:border-gray-400 transition-colors text-center shadow-sm"
                        >
                            Admin
                        </button>
                    </div>
                </div>

                <form className="mt-6 space-y-6" onSubmit={handleSubmit(onSubmit)}>
                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}
                    <div className="rounded-md shadow-sm space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Username or Email</label>
                            <input
                                type="text"
                                {...register('username', { required: 'Username or Email is required' })}
                                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-gray-900 focus:border-gray-900 focus:z-10 sm:text-sm mt-1"
                                placeholder="Enter your username or email"
                            />
                            {errors.username && <span className="text-xs text-red-500">{errors.username.message}</span>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Password</label>
                            <input
                                type="password"
                                {...register('password', { required: 'Password is required' })}
                                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-gray-900 focus:border-gray-900 focus:z-10 sm:text-sm mt-1"
                                placeholder="Enter your password"
                            />
                            {errors.password && <span className="text-xs text-red-500">{errors.password.message}</span>}
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-gray-900 hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-900 disabled:opacity-50 transition-colors"
                        >
                            {isLoading ? 'Signing in...' : 'Sign in'}
                        </button>
                    </div>
                    <div className="text-sm text-center">
                        <Link to="/register" className="font-medium text-gray-900 hover:text-gray-600 underline">
                            Don't have an account? Register here
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
