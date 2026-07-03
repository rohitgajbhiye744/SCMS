import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useForm } from 'react-hook-form';

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors } } = useForm();
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

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
                <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
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
